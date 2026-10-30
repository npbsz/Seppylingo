import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OptionsGrid } from './options-grid';

describe('OptionsGrid', () => {
  let component: OptionsGrid;
  let fixture: ComponentFixture<OptionsGrid>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OptionsGrid]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OptionsGrid);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
